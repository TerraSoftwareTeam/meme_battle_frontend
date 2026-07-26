const https = require('https');

// Force IPv4 agent to fix ENETUNREACH on systems with broken IPv6 routing
const ipv4Agent = new https.Agent({ family: 4 });

config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ['/cdn-proxy'],
        target: 'https://user-cdn.hackclub-assets.com',
        changeOrigin: true,
        secure: false,
        pathRewrite: { '^/cdn-proxy': '' },
        agent: ipv4Agent,
        onProxyRes: function(proxyRes) {
            proxyRes.headers['access-control-allow-origin'] = '*';
        }
    },
    {
        context: ['/api-proxy'],
        target: 'https://meme.skyfly.hackclub.app',
        changeOrigin: true,
        secure: true,
        pathRewrite: { '^/api-proxy': '' },
        agent: ipv4Agent
    },
    {
        context: ['/ws-proxy'],
        target: 'wss://realtime.meme.skyfly.hackclub.app',
        changeOrigin: true,
        secure: true,
        ws: true,
        pathRewrite: { '^/ws-proxy': '' },
        agent: ipv4Agent
    }
];
