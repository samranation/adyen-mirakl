(function() {
    'use strict';
    angular
        .module('adyenMiraklConnectorApp')
        .factory('AdyenPayoutError', AdyenPayoutError);

    AdyenPayoutError.$inject = ['$resource', 'DateUtils'];

    function AdyenPayoutError ($resource, DateUtils) {
        var resourceUrl =  'api/adyen-payout-errors/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.createdAt = DateUtils.convertDateTimeFromServer(data.createdAt);
                        data.updatedAt = DateUtils.convertDateTimeFromServer(data.updatedAt);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
