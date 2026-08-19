const https = require('https');

// Force IPv4 agent to fix ENETUNREACH on systems with broken IPv6 routing
// https.Agent is for HTTP/HTTPS only; use tls.Agent for WebSocket (wss://) proxying.
const ipv4HttpAgent = new https.Agent({ family: 4 });

config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ['/cdn-proxy'],
        target: 'https://user-cdn.hackclub-assets.com',
        changeOrigin: true,
        secure: false,
        pathRewrite: { '^/cdn-proxy': '' },
        agent: ipv4HttpAgent,
        onProxyRes: function(proxyRes) {
            proxyRes.headers['access-control-allow-origin'] = '*';
        }
    },
    {
        context: ['/api-proxy'],
        target: 'https://api.meme.skyfly.hackclub.app',
        changeOrigin: true,
        secure: true,
        pathRewrite: { '^/api-proxy': '' },
        agent: ipv4HttpAgent
    },
    {
        context: ['/ws-proxy'],
        target: 'wss://realtime.meme.skyfly.hackclub.app',
        changeOrigin: true,
        secure: true,
        ws: true,
        pathRewrite: { '^/ws-proxy': '' },
        // Do NOT pass agent here: http-proxy-middleware handles wss:// natively.
        // Passing https.Agent here breaks the WebSocket upgrade (causes 1011).
        onProxyReqWs: function(proxyReq, req, socket, options, head) {
            console.log('[WEBPACK-PROXY] WebSocket upgrade requested for:', req.url);
            console.log('[WEBPACK-PROXY] Target:', options.target);
            // Centrifugo rejects localhost Origin, remove it entirely
            proxyReq.removeHeader('origin');
        },
        onProxyRes: function(proxyRes, req, res) {
            console.log('[WEBPACK-PROXY] Response received:', proxyRes.statusCode);
        },
        onError: function(err, req, res) {
            console.error('[WEBPACK-PROXY] Error:', err.message);
        }
    }
];
