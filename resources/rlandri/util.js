rlandri.util = {
    parseJSON: function (data, defValue) {
        var obj = defValue;
        try {
            if (data)
                obj = jQuery.parseJSON(data);
        } catch (e) {
            console.log(e);
        }
        return obj;
    },

    trimSuffix: function (str, suffix) {
        var index = str.lastIndexOf(suffix);
        if (index >= 0)
            return str.substring(0, index);
        else
            return '';
    },

    AttrIndexManager: function (attr) {
        this.names = { };
        this.maxIndex = 0;
        this.attr = attr;
    },
};

rlandri.util.AttrIndexManager.prototype.updateAttr = function (elem) {
    var name = elem.name;
    var index;
    if (name in this.names) {
        index = this.names[name];
    } else {
        index = this.maxIndex;
        this.names[name] = this.maxIndex;
        this.maxIndex++;
    }
    elem.setAttribute(this.attr, index);
}

rlandri.util.AttrIndexManager.prototype.updateObj = function ($obj) {
    var manager = this;
    $obj.each(function () {
        manager.updateAttr(this);
    });
}
