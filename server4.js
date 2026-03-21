const http = require('http');
const fs   = require('fs');
const path = require('path');
const dir  = path.join(__dirname);
const MIME = {
  '.html':'text/html','.css':'text/css','.js':'application/javascript',
  '.json':'application/json','.png':'image/png','.svg':'image/svg+xml',
  '.ico':'image/x-icon','.webmanifest':'application/manifest+json',
};

http.createServer((req, res) => {
  let url  = req.url.split('?')[0];
  let file = path.join(dir, url === '/' ? 'index.html' : url);
  if (!fs.existsSync(file)) { res.writeHead(404); res.end('Not found'); return; }
  const ct = MIME[path.extname(file)] || 'text/plain';
  res.writeHead(200, { 'Content-Type': ct, 'Cache-Control': 'no-cache' });
  fs.createReadStream(file).pipe(res);
}).listen(3003, () => {
  console.log('Kipita dev server → http://localhost:3003');
});
