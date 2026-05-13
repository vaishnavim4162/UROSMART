import os
import xml.etree.ElementTree as ET
from collections import Counter

src_annos = r"c:\Users\Vishnu\OneDrive\Desktop\taining model\Annotations-20260506T174623Z-3-001\Annotations"
names = []

for filename in os.listdir(src_annos):
    if filename.endswith(".xml"):
        try:
            tree = ET.parse(os.path.join(src_annos, filename))
            root = tree.getroot()
            for obj in root.findall('object'):
                name = obj.find('name').text
                names.append(name)
        except Exception as e:
            print(f"Error parsing {filename}: {e}")

counts = Counter(names)
for name, count in sorted(counts.items()):
    print(f"{name}: {count}")
