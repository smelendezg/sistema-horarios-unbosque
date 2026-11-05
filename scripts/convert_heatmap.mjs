import fs from 'node:fs/promises';
import sharp from 'sharp';
import {fileURLToPath} from 'node:url';
const input=new URL('../docs/mapa-calor-riesgos.svg',import.meta.url);
const output=new URL('../.artifacts/heatmap.png',import.meta.url);
await fs.mkdir(new URL('../.artifacts/',import.meta.url),{recursive:true});
await sharp(await fs.readFile(input),{density:180}).png().toFile(fileURLToPath(output));
