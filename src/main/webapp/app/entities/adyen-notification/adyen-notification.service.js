(function() {
    'use strict';
    angular
        .module('adyenMiraklConnectorApp')
        .factory('AdyenNotification', AdyenNotification);

    AdyenNotification.$inject = ['$resource'];

    function AdyenNotification ($resource) {
        var resourceUrl =  'api/adyen-notifications/:id';

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
