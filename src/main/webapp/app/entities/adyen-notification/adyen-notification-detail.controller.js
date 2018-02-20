(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenNotificationDetailController', AdyenNotificationDetailController);

    AdyenNotificationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'AdyenNotification'];

    function AdyenNotificationDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, AdyenNotification) {
        var vm = this;

        vm.adyenNotification = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('adyenMiraklConnectorApp:adyenNotificationUpdate', function(event, result) {
            vm.adyenNotification = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
