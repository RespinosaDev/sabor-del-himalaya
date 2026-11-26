"""Recuento de LOC.
Cuenta las líneas físicas con código (sin líneas en blanco ni solo comentarios)
"""
import os, re, sys, json
root = sys.argv[1] if len(sys.argv) > 1 else '.'
def count_java_like(text, line_c='//', block=('/*', '*/')):
    n = 0; inblock = False
    for raw in text.splitlines():
        l = raw.strip()
        if inblock:
            if block[1] in l:
                inblock = False; rest = l.split(block[1], 1)[1].strip()
                if rest and not rest.startswith(line_c): n += 1
            continue
        if not l or (line_c and l.startswith(line_c)): continue
        if l.startswith(block[0]):
            if block[1] not in l[len(block[0]):]: inblock = True
            continue
        n += 1
    return n
def count_html(text):
    t = re.sub(r'<!--.*?-->', '', text, flags=re.S)
    return sum(1 for l in t.splitlines() if l.strip())
def count_props(text):
    return sum(1 for l in text.splitlines() if l.strip() and not l.strip().startswith('#'))
res = {}
for d, _, fs in os.walk(root):
    if '/target' in d or '/data' in d: continue
    for f in fs:
        p = os.path.join(d, f); rel = os.path.relpath(p, root); txt = open(p, encoding='utf8').read()
        if f.endswith('.java'):
            cat = 'Java (pruebas)' if '/test/' in p else 'Java (aplicación)'; n = count_java_like(txt)
        elif f.endswith('.html'): cat = 'HTML/Thymeleaf'; n = count_html(txt)
        elif f.endswith('.css'): cat = 'CSS'; n = count_java_like(txt, line_c=None)
        elif f.endswith('.properties'): cat = 'Configuración (.properties)'; n = count_props(txt)
        elif f == 'pom.xml': cat = 'Configuración (pom.xml)'; n = count_html(txt)
        else: continue
        res.setdefault(cat, []).append((rel, n))
out = {k: {'ficheros': len(v), 'loc': sum(n for _, n in v), 'detalle': sorted(v)} for k, v in res.items()}
producto = sum(v['loc'] for k, v in out.items() if k in ('Java (aplicación)', 'HTML/Thymeleaf', 'CSS'))
for k, v in sorted(out.items()):
    print(f"{k:30s} {v['ficheros']:4d} ficheros {v['loc']:6d} LOC")
print(f"{'LOC del producto':30s} {producto:20d} LOC")
