rlandri = {
    dynamic_scripts: {
        addCSS: function (path) {
            var type = 'text/css';
            var rel = 'stylesheet';
            var tpl = "%3Clink href='{0}' rel='{1}' type='{2}'%3E";
            tpl = tpl.replace("{0}", path);
            tpl = tpl.replace("{1}", rel);
            tpl = tpl.replace("{2}", type);
            document.write(unescape(tpl));
        },

        addJavascript: function (path) {
            var type = 'text/javascript';
            var tpl = "%3Cscript src='{0}' type='{1}'%3E%3C/script%3E";
            tpl = tpl.replace("{0}", path);
            tpl = tpl.replace("{1}", type);
            document.write(unescape(tpl));
        },

        dir: '/resources/fallback/',
    },
};
