"""Align extracted generated artwork using one fixed scale per complete row."""
from pathlib import Path
import json
from PIL import Image

root = Path(__file__).parent
states = ['idle','running-right','running-left','waving','jumping','failed','waiting','running','review']
report = {}
for state in states:
    files = sorted((root/'qa/rows'/state/'frames'/state).glob('*.png'))
    if not files:
        raise SystemExit(f'Missing {state}')
    images = [Image.open(f).convert('RGBA') for f in files]
    bounds = [im.getchannel('A').point(lambda a: 255 if a > 16 else 0).getbbox() for im in images]
    # Match the resting pose's size while retaining all intra-row shape and height changes.
    resting = bounds[0]
    factor = 154 / (resting[3]-resting[1])
    source_ground = max(b[3] for b in bounds)
    destination = root/'frames'/state
    destination.mkdir(parents=True, exist_ok=True)
    for file, im in zip(files, images):
        resized = im.resize((round(im.width*factor),round(im.height*factor)),Image.Resampling.LANCZOS)
        cell = Image.new('RGBA',(192,208))
        cell.alpha_composite(resized,(round(96-im.width*factor/2),round(198-source_ground*factor)))
        cell.save(destination/file.name)
    report[state]={'shared_row_scale':factor,'source_baseline':source_ground,'target_baseline':198,'target_resting_height':154}
(root/'qa/standard-registration.json').write_text(json.dumps(report,indent=2))
