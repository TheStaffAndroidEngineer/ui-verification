#!/usr/bin/env python3
"""Execute known defects separately; validate their exact failure witnesses."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--java-home', required=True)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
project = Path(__file__).resolve().parents[1]
out = args.output.resolve()
out.mkdir(parents=True, exist_ok=True)
package = 'staff.android.uiverification'
cases = [
    ('navigation-mutant', ['-PbrokenReturn'], 2, 1, 'Completed editor must be absent'),
    ('media-first-visible', ['-PtestClass=' + package + '.solution.EpisodeFirstVisibleMutantTest'], 1, 1, 'before reorder'),
    ('media-initial-position', ['-PtestClass=' + package + '.solution.EpisodeInitialPositionMutantTest'], 1, 1, 'after reorder'),
    ('media-progress', ['-PtestClass=' + package + '.solution.EpisodeProgressMutantTest'], 1, 1, 'progress is not an action'),
    ('learner', ['-PtestClass=' + package + '.exercise.EpisodeLearnerTest'], 4, 0, None),
]
result = {'serial': args.serial, 'passed': False, 'runs': []}
environment = dict(os.environ, ANDROID_SERIAL=args.serial, JAVA_HOME=args.java_home)
try:
    for name, options, expected_count, expected_failures, witness in cases:
        target = out / name
        target.mkdir(exist_ok=True)
        command = [str(project / 'gradlew'), '-p', str(project),
            ':app:connectedDebugAndroidTest', '--rerun-tasks', '--no-build-cache', *options]
        with (target / 'run.log').open('w') as log:
            run = subprocess.run(command, env=environment, stdout=log, stderr=subprocess.STDOUT)
        shutil.copytree(project / 'app/build/outputs/androidTest-results',
            target / 'android', dirs_exist_ok=True)
        tests = [t for file in (target / 'android').rglob('TEST-*.xml')
            for t in ET.parse(file).getroot().iter('testcase')]
        failures = [t.find('failure') for t in tests if t.find('failure') is not None]
        errors = [t.find('error') for t in tests if t.find('error') is not None]
        skips = [t.find('skipped') for t in tests if t.find('skipped') is not None]
        observed = {'name': name, 'command': command, 'exit': run.returncode,
            'tests': len(tests), 'failures': len(failures), 'errors': len(errors),
            'skipped': len(skips), 'failure_witness': witness}
        result['runs'].append(observed)
        assert len(tests) == expected_count and len(failures) == expected_failures
        assert not errors and not skips
        assert run.returncode == (1 if expected_failures else 0)
        if witness:
            assert witness in ''.join((f.text or '') + f.get('message', '') for f in failures)
        print(json.dumps(observed), flush=True)
    result['passed'] = True
finally:
    (out / 'results.json').write_text(json.dumps(result, indent=2) + '\n')
