(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenPayoutErrorDetailController', AdyenPayoutErrorDetailController);

    AdyenPayoutErrorDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'AdyenPayoutError'];

    function AdyenPayoutErrorDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, AdyenPayoutError) {
        var vm = this;

        vm.adyenPayoutError = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('adyenMiraklConnectorApp:adyenPayoutErrorUpdate', function(event, result) {
            vm.adyenPayoutError = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
