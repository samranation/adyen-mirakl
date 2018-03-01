(function() {
    'use strict';
    angular
        .module('adyenMiraklConnectorApp')
        .factory('MiraklDelta', MiraklDelta);

    MiraklDelta.$inject = ['$resource', 'DateUtils'];

    function MiraklDelta ($resource, DateUtils) {
        var resourceUrl =  'api/mirakl-deltas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.shopDelta = DateUtils.convertDateTimeFromServer(data.shopDelta);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
