#!/usr/bin/env python3
"""
mvn javadoc:javadoc


Scan Java source files and calculate Javadoc coverage
Usage: python javadoc_coverage_scanner.py <path_to_src_folder>
Example: python javadoc_coverage_scanner.py src/main/java/fr/diginamic/gestiondestransports
"""

import os
import re
import sys
from pathlib import Path
from collections import defaultdict

class JavadocScanner:
    def __init__(self):
        self.stats = defaultdict(lambda: {
            'total_classes': 0,
            'documented_classes': 0,
            'total_methods': 0,
            'documented_methods': 0,
            'files': []
        })

    def has_javadoc_before_line(self, lines, line_index):
        """Check if there's a Javadoc comment before the given line"""
        # Look backwards from the line, skipping annotations and whitespace
        for i in range(line_index - 1, max(0, line_index - 20), -1):
            line = lines[i].strip()

            # Found Javadoc start
            if line.startswith('/**'):
                return True

            # Skip these lines and keep looking
            if (not line or  # Empty line
                line.startswith('*') or  # Inside a comment
                line.startswith('//') or  # Single line comment
                line.startswith('@') or  # Annotation
                line == '}' or  # Closing brace
                line.startswith('import ') or  # Import statement
                line.startswith('package ')):  # Package statement
                continue

            # Found actual code (not annotation, not comment) - stop looking
            if line and not line.startswith('*'):
                return False

        return False

    def scan_file(self, file_path):
        """Scan a single Java file"""
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                lines = f.readlines()
        except Exception as e:
            print(f"❌ Erreur lecture {file_path}: {e}")
            return None

        package_name = "default"
        class_name = None
        file_stats = {
            'classes': 0,
            'documented_classes': 0,
            'methods': 0,
            'documented_methods': 0,
            'details': []
        }

        # Find package
        for line in lines:
            package_match = re.search(r'package\s+([\w.]+);', line)
            if package_match:
                package_name = package_match.group(1)
                break

        # Scan for classes and methods
        for i, line in enumerate(lines):
            stripped = line.strip()

            # Public class
            class_match = re.search(r'public\s+(class|interface|enum)\s+(\w+)', stripped)
            if class_match:
                class_name = class_match.group(2)
                file_stats['classes'] += 1
                has_doc = self.has_javadoc_before_line(lines, i)
                if has_doc:
                    file_stats['documented_classes'] += 1
                file_stats['details'].append({
                    'type': 'class',
                    'name': class_name,
                    'documented': has_doc,
                    'line': i + 1
                })

            # Public methods (skip constructors for now)
            method_match = re.search(r'public\s+(?!class|interface|enum)(\w+(?:<.*?>)?)\s+(\w+)\s*\(', stripped)
            if method_match and class_name:
                method_return = method_match.group(1)
                method_name = method_match.group(2)

                # Skip constructors (method name == class name)
                if method_name != class_name:
                    file_stats['methods'] += 1
                    has_doc = self.has_javadoc_before_line(lines, i)
                    if has_doc:
                        file_stats['documented_methods'] += 1
                    file_stats['details'].append({
                        'type': 'method',
                        'name': f"{method_name}()",
                        'return_type': method_return,
                        'documented': has_doc,
                        'line': i + 1
                    })

        # Update package stats
        if file_stats['classes'] > 0 or file_stats['methods'] > 0:
            self.stats[package_name]['total_classes'] += file_stats['classes']
            self.stats[package_name]['documented_classes'] += file_stats['documented_classes']
            self.stats[package_name]['total_methods'] += file_stats['methods']
            self.stats[package_name]['documented_methods'] += file_stats['documented_methods']
            self.stats[package_name]['files'].append({
                'path': str(file_path),
                'name': os.path.basename(file_path),
                **file_stats
            })

        return file_stats

    def scan_directory(self, directory):
        """Scan all Java files in directory"""
        java_files = Path(directory).rglob('*.java')
        for java_file in java_files:
            self.scan_file(java_file)

    def generate_report(self):
        """Generate markdown report"""
        report = []
        report.append("# 📊 Rapport de Couverture Javadoc\n")
        report.append(f"*Généré automatiquement par scan des fichiers sources*\n")

        # Calculate totals
        total_classes = sum(p['total_classes'] for p in self.stats.values())
        documented_classes = sum(p['documented_classes'] for p in self.stats.values())
        total_methods = sum(p['total_methods'] for p in self.stats.values())
        documented_methods = sum(p['documented_methods'] for p in self.stats.values())

        class_coverage = (documented_classes / total_classes * 100) if total_classes > 0 else 0
        method_coverage = (documented_methods / total_methods * 100) if total_methods > 0 else 0
        overall_coverage = ((documented_classes + documented_methods) / (total_classes + total_methods) * 100) if (total_classes + total_methods) > 0 else 0

        # Summary
        report.append("## 📈 Résumé Global\n")
        report.append(f"- **Couverture Globale**: {overall_coverage:.1f}%")
        report.append(f"- **Classes**: {documented_classes}/{total_classes} ({class_coverage:.1f}%)")
        report.append(f"- **Méthodes**: {documented_methods}/{total_methods} ({method_coverage:.1f}%)")
        report.append(f"- **Packages analysés**: {len(self.stats)}\n")

        # Status
        if overall_coverage == 100:
            status = "✅ EXCELLENT"
        elif overall_coverage >= 80:
            status = "✅ BON"
        elif overall_coverage >= 50:
            status = "⚠️ MOYEN"
        else:
            status = "❌ INSUFFISANT"

        report.append(f"**Statut**: {status}\n")

        # By package
        report.append("## 📦 Couverture par Package\n")
        report.append("| Package | Classes | Méthodes | Couverture | Statut |")
        report.append("|---------|---------|----------|------------|--------|")

        for package_name in sorted(self.stats.keys()):
            pkg = self.stats[package_name]
            pkg_total = pkg['total_classes'] + pkg['total_methods']
            pkg_doc = pkg['documented_classes'] + pkg['documented_methods']
            pkg_coverage = (pkg_doc / pkg_total * 100) if pkg_total > 0 else 0

            status_icon = "✅" if pkg_coverage == 100 else "⚠️" if pkg_coverage >= 50 else "❌"

            report.append(f"| {package_name.split('.')[-1]} | {pkg['documented_classes']}/{pkg['total_classes']} | {pkg['documented_methods']}/{pkg['total_methods']} | {pkg_coverage:.1f}% | {status_icon} |")

        report.append("")

        # Detailed by file
        report.append("## 📄 Détails par Fichier\n")

        for package_name in sorted(self.stats.keys()):
            pkg = self.stats[package_name]
            if pkg['files']:
                report.append(f"### Package: {package_name}\n")

                for file_info in sorted(pkg['files'], key=lambda x: x['name']):
                    file_total = file_info['classes'] + file_info['methods']
                    file_doc = file_info['documented_classes'] + file_info['documented_methods']
                    file_coverage = (file_doc / file_total * 100) if file_total > 0 else 0

                    status_icon = "✅" if file_coverage == 100 else "⚠️" if file_coverage >= 50 else "❌"

                    report.append(f"#### {status_icon} {file_info['name']} ({file_coverage:.0f}%)\n")

                    # Show undocumented items
                    undocumented = [d for d in file_info['details'] if not d['documented']]
                    if undocumented:
                        report.append("**Non documenté**:")
                        for item in undocumented:
                            if item['type'] == 'class':
                                report.append(f"- 🔴 Classe `{item['name']}` (ligne {item['line']})")
                            else:
                                report.append(f"- 🔴 Méthode `{item['name']}` (ligne {item['line']})")
                        report.append("")

        # Action plan
        report.append("## 🎯 Plan d'Action\n")

        undoc_classes = total_classes - documented_classes
        undoc_methods = total_methods - documented_methods

        if undoc_classes > 0:
            report.append(f"1. **Documenter {undoc_classes} classe(s)**")
        if undoc_methods > 0:
            report.append(f"2. **Documenter {undoc_methods} méthode(s)**")

        if overall_coverage < 100:
            report.append("\n**Recommandations**:")
            report.append("- Utiliser `add_javadoc.py` pour automatiser les getters/setters")
            report.append("- Documenter manuellement les méthodes métier importantes")
            report.append("- Prioriser les classes de service (mentionnées dans le cahier des charges)")
        else:
            report.append("\n🎉 **Félicitations! Votre code est documenté à 100%!**")

        return "\n".join(report)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python javadoc_coverage_scanner.py <chemin_vers_src>")
        print("\nExemples:")
        print("  python javadoc_coverage_scanner.py src/main/java")
        print("  python javadoc_coverage_scanner.py src/main/java/fr/diginamic/gestiondestransports/services")
        sys.exit(1)

    directory = sys.argv[1]

    if not os.path.isdir(directory):
        print(f"❌ Erreur: {directory} n'est pas un répertoire valide")
        sys.exit(1)

    print(f"🔍 Scan en cours de {directory}...")

    scanner = JavadocScanner()
    scanner.scan_directory(directory)

    if not scanner.stats:
        print("❌ Aucun fichier Java trouvé")
        sys.exit(1)

    report = scanner.generate_report()

    # Save report
    output_file = "javadoc_coverage_detailed.md"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(report)

    print(f"✅ Rapport généré: {output_file}")

    # Print summary
    total_classes = sum(p['total_classes'] for p in scanner.stats.values())
    documented_classes = sum(p['documented_classes'] for p in scanner.stats.values())
    total_methods = sum(p['total_methods'] for p in scanner.stats.values())
    documented_methods = sum(p['documented_methods'] for p in scanner.stats.values())
    overall_coverage = ((documented_classes + documented_methods) / (total_classes + total_methods) * 100) if (total_classes + total_methods) > 0 else 0

    print(f"\n📊 Résumé:")
    print(f"   - Classes: {documented_classes}/{total_classes}")
    print(f"   - Méthodes: {documented_methods}/{total_methods}")
    print(f"   - Couverture: {overall_coverage:.1f}%")