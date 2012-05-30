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

    constructPrimaryMap: function (dataArr) {
        var dataMap = { };
        for (var i in dataArr) {
            data = dataArr[i];
            dataMap[data.pk] = data;
        }
        return dataMap;
    },

    getModelField: function (obj, key) {
        var value;
        if (obj) {
            var fields = obj.fields;
            if (fields) {
                value = fields[key];
            }
        }
        return value;
    },

    trimSuffix: function (str, suffix) {
        var index = str.lastIndexOf(suffix);
        if (index >= 0)
            return str.substring(0, index);
        else
            return '';
    },
};
