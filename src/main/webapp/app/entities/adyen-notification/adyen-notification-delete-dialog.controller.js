(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenNotificationDeleteController',AdyenNotificationDeleteController);

    AdyenNotificationDeleteController.$inject = ['$uibModalInstance', 'entity', 'AdyenNotification'];

    function AdyenNotificationDeleteController($uibModalInstance, entity, AdyenNotification) {
        var vm = this;

        vm.adyenNotification = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            AdyenNotification.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
