#!/usr/bin/env python3
"""Converts flx-reference.md to the HTML page the demo app shows in its FLX Reference screen.

    python3 docs/reference/build_reference_html.py

Reads docs/reference/flx-reference.md and writes app/src/main/assets/flx-reference.html. Needs the
`markdown` package (pip install markdown). Run it again whenever the Markdown changes and commit
both files.

The page takes its colours from CSS variables. The app replaces the block between /*FLX_THEME*/ and /*END_FLX_THEME*/ with the
variables of the theme selected in Settings, and updates the same <style id="flx-theme"> element
when the theme changes, so the page follows the app's theme.
"""
import re
import sys
from pathlib import Path

import markdown

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "docs" / "reference" / "flx-reference.md"
TARGET = ROOT / "app" / "src" / "main" / "assets" / "flx-reference.html"


def github_slug(value: str, separator: str) -> str:
    """The heading ids GitHub makes, so the links in the Markdown work in the page too."""
    value = value.strip().lower()
    value = re.sub(r"[^\w\- ]", "", value)
    return value.replace(" ", separator)


PAGE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>FLX Reference</title>
<style id="flx-theme">/*FLX_THEME*/</style>
<style>
html { -webkit-text-size-adjust: 100%; }
body { margin: 0; background: var(--flx-bg); color: var(--flx-fg);
       font: 16px/1.6 system-ui, -apple-system, Roboto, sans-serif; overflow-wrap: anywhere; }
main { max-width: 52rem; margin: 0 auto; padding: 8px 16px 64px; }
h1, h2, h3 { line-height: 1.25; }
h1 { font-size: 1.6em; margin: 1.8em 0 .6em; padding-bottom: .3em; border-bottom: 1px solid var(--flx-border); }
h2 { font-size: 1.3em; margin: 1.6em 0 .6em; }
h3 { font-size: 1.1em; margin: 1.4em 0 .4em; }
a { color: var(--flx-primary); }
code { font-family: ui-monospace, "Roboto Mono", monospace; font-size: .9em;
       background: var(--flx-code-bg); padding: .1em .3em; border-radius: 4px; }
pre { background: var(--flx-code-bg); padding: 12px; border-radius: 8px; overflow-x: auto; }
pre code { background: none; padding: 0; font-size: .85em; white-space: pre; }
table { border-collapse: collapse; display: block; overflow-x: auto; max-width: 100%; }
th, td { border: 1px solid var(--flx-border); padding: 6px 10px; text-align: left; vertical-align: top; }
th { background: var(--flx-code-bg); }
hr { border: 0; border-top: 1px solid var(--flx-border); margin: 2em 0; }
ul, ol { padding-left: 1.4em; }
</style>
</head>
<body>
<main>
@@BODY@@
</main>
</body>
</html>
"""

LIGHT_DEFAULTS = (
    ":root { color-scheme: light; --flx-bg: #ffffff; --flx-fg: #1b1b1f; --flx-primary: #2b5bd7; "
    "--flx-code-bg: #eef0f4; --flx-border: #c5c6d0; }"
)


def main() -> int:
    body = markdown.markdown(
        SOURCE.read_text(encoding="utf-8"),
        extensions=["tables", "fenced_code", "toc"],
        extension_configs={"toc": {"slugify": github_slug}},
    )
    # A link to a section in the page is made a flx-anchor:ID link. The WebView does not report
    # plain #anchor links to the app, which has to know about them to keep a way back.
    body = re.sub(r'href="#([^"]+)"', r'href="flx-anchor:\1"', body)
    html = PAGE.replace("@@BODY@@", body).replace(
        "/*FLX_THEME*/", "/*FLX_THEME*/" + LIGHT_DEFAULTS + "/*END_FLX_THEME*/"
    )
    TARGET.write_text(html, encoding="utf-8")
    print(f"{TARGET.relative_to(ROOT)}: {len(html)} bytes")
    return 0


if __name__ == "__main__":
    sys.exit(main())
