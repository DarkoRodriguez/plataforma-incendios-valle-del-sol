#!/usr/bin/env python3
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET
import re

md = Path('doc/Informe_Final.md').read_text(encoding='utf-8')
lines = md.splitlines()

# XML helper
W_NS = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
ET.register_namespace('w', W_NS)

def make_paragraph(text, bold=False):
    p = ET.Element(f'{{{W_NS}}}p')
    r = ET.SubElement(p, f'{{{W_NS}}}r')
    if bold:
        rpr = ET.SubElement(r, f'{{{W_NS}}}rPr')
        ET.SubElement(rpr, f'{{{W_NS}}}b')
    t = ET.SubElement(r, f'{{{W_NS}}}t')
    # xml:space preserve for leading/trailing spaces
    t.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    t.text = text
    return p

body = ET.Element(f'{{{W_NS}}}body')

for line in lines:
    if not line.strip():
        body.append(make_paragraph(''))
        continue
    if line.startswith('# '):
        body.append(make_paragraph(line[2:].strip(), bold=True))
    elif line.startswith('## '):
        body.append(make_paragraph(line[3:].strip(), bold=True))
    elif re.match(r'^\s*-\s', line):
        text = line.lstrip(' -')
        body.append(make_paragraph('• ' + text))
    else:
        # normal paragraph, strip md inline markers
        text = re.sub(r'\*\*(.*?)\*\*', r'\1', line)
        text = re.sub(r'`(.*?)`', r'\1', text)
        body.append(make_paragraph(text))

# minimal section properties
sectPr = ET.SubElement(body, f'{{{W_NS}}}sectPr')
pgSz = ET.SubElement(sectPr, f'{{{W_NS}}}pgSz')
pgSz.set(f'{{{W_NS}}}w', '12240')
pgSz.set(f'{{{W_NS}}}h', '15840')
pgMar = ET.SubElement(sectPr, f'{{{W_NS}}}pgMar')
pgMar.set(f'{{{W_NS}}}top', '1440')
pgMar.set(f'{{{W_NS}}}right', '1440')
pgMar.set(f'{{{W_NS}}}bottom', '1440')
pgMar.set(f'{{{W_NS}}}left', '1440')

# build document xml
document = ET.Element(f'{{{W_NS}}}document')
document.append(body)

doc_xml = ET.tostring(document, encoding='utf-8', xml_declaration=True, method='xml')

content_types = b'''<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>'''

rels = b'''<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>'''

# create zip (docx)
out_path = Path('doc/Informe_Final.docx')
with zipfile.ZipFile(out_path, 'w', compression=zipfile.ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml', content_types)
    z.writestr('_rels/.rels', rels)
    z.writestr('word/document.xml', doc_xml)

print('WROTE', out_path)
