(function() {
    'use strict';
    angular
        .module('adyenMiraklConnectorApp')
        .factory('ProcessEmail', ProcessEmail);

    ProcessEmail.$inject = ['$resource'];

    function ProcessEmail ($resource) {
        var resourceUrl =  'api/process-emails/:id';

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
