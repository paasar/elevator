angular.module('gameFilters', []).filter('nameFilter', function() {
    return function(items, search) {
        if (search === undefined) {
            return items;
        } else {
            var filtered = [];

            angular.forEach(items, function(item) {
                var expression = search.client.name;
                var re = new RegExp(expression, "g");
                if (item.client.name.match(re) !== null) {
                    filtered.push(item);
                }
            });

            return filtered;
        }
    };
});