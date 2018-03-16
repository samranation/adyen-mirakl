(function() {
    'use strict';
    angular
        .module('adyenMiraklConnectorApp')
        .factory('ShareholderMapping', ShareholderMapping);

    ShareholderMapping.$inject = ['$resource'];

    function ShareholderMapping ($resource) {
        var resourceUrl =  'api/shareholder-mappings/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
