"""Tests de la politique locale de chaîne logicielle (python -m unittest discover scripts)."""
import unittest

from course import actions_mutables, from_mutables

SHA = "3d3c42e5aac5ba805825da76410c181273ba90b1"
DIGEST = "sha256:" + "a" * 64


class ActionsMutablesTest(unittest.TestCase):
    def test_action_epinglee_par_sha_acceptee(self):
        self.assertEqual(actions_mutables(f"      - uses: actions/checkout@{SHA} # v7.0.1\n"), [])

    def test_action_par_tag_refusee(self):
        self.assertEqual(actions_mutables("      - uses: actions/checkout@v7\n"), ["actions/checkout@v7"])

    def test_action_par_branche_refusee(self):
        self.assertEqual(actions_mutables("        uses: 'aquasecurity/trivy-action@master'\n"),
                         ["aquasecurity/trivy-action@master"])

    def test_sha_court_refuse(self):
        self.assertEqual(actions_mutables("      - uses: actions/checkout@3d3c42e\n"), ["actions/checkout@3d3c42e"])

    def test_action_locale_ignoree(self):
        self.assertEqual(actions_mutables("      - uses: ./.github/actions/locale\n"), [])


class FromMutablesTest(unittest.TestCase):
    def test_from_tag_et_digest_accepte(self):
        self.assertEqual(from_mutables(f"FROM eclipse-temurin:21-jre@{DIGEST}\n"), [])

    def test_from_tag_seul_refuse(self):
        self.assertEqual(from_mutables("FROM eclipse-temurin:21-jre\n"), ["eclipse-temurin:21-jre"])

    def test_option_platform_et_alias_geres(self):
        texte = f"FROM --platform=$BUILDPLATFORM node:22-alpine@{DIGEST} AS build\n"
        self.assertEqual(from_mutables(texte), [])

    def test_option_platform_sans_digest_refusee(self):
        self.assertEqual(from_mutables("FROM --platform=$BUILDPLATFORM node:22-alpine AS build\n"),
                         ["node:22-alpine"])

    def test_reference_a_une_etape_precedente_acceptee(self):
        texte = f"FROM node:22-alpine@{DIGEST} AS build\nFROM build\n"
        self.assertEqual(from_mutables(texte), [])

    def test_casse_et_espaces(self):
        self.assertEqual(from_mutables("  from   nginx:1.31\n"), ["nginx:1.31"])


if __name__ == "__main__":
    unittest.main()
