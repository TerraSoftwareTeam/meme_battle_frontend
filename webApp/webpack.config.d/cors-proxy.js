// CORS proxy for image CDN
// The CDN at user-cdn.hackclub-assets.com returns "Access-Control-Allow-Origin: *, *"
// which browsers reject. We proxy through the local dev server to fix the headers.
config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ['/cdn-proxy'],
        target: 'https://user-cdn.hackclub-assets.com',
        changeOrigin: true,
        secure: false,
        pathRewrite: { '^/cdn-proxy': '' },
        onProxyRes: function(proxyRes) {
            proxyRes.headers['access-control-allow-origin'] = '*';
        },
        on: {
            proxyRes: function(proxyRes) {
                proxyRes.headers['access-control-allow-origin'] = '*';
            }
        }
    }
];
