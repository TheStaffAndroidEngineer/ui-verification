#!/usr/bin/env python3
"""Exact-pixel comparison under a recorded, fixed rendering configuration.

Requires Pillow 12.1 or later. Never replaces the reviewed reference; writes only the report.
"""
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image, __version__ as pillow_version

parser = argparse.ArgumentParser()
parser.add_argument('reference', type=Path)
parser.add_argument('candidate', type=Path)
parser.add_argument('--report', type=Path, required=True)
args = parser.parse_args()
reference = Image.open(args.reference).convert('RGBA')
candidate = Image.open(args.candidate).convert('RGBA')
equal_size = reference.size == candidate.size
changed = sum(a != b for a, b in zip(reference.get_flattened_data(), candidate.get_flattened_data())) if equal_size else None
report = {
    'reference': str(args.reference), 'candidate': str(args.candidate),
    'reference_sha256': hashlib.sha256(args.reference.read_bytes()).hexdigest(),
    'candidate_sha256': hashlib.sha256(args.candidate.read_bytes()).hexdigest(),
    'reference_size': reference.size, 'candidate_size': candidate.size,
    'changed_pixels': changed, 'pillow_version': pillow_version,
    'passed': equal_size and changed == 0,
}
args.report.parent.mkdir(parents=True, exist_ok=True)
args.report.write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report))
raise SystemExit(0 if report['passed'] else 1)
