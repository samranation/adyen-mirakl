(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenPayoutErrorDeleteController',AdyenPayoutErrorDeleteController);

    AdyenPayoutErrorDeleteController.$inject = ['$uibModalInstance', 'entity', 'AdyenPayoutError'];

    function AdyenPayoutErrorDeleteController($uibModalInstance, entity, AdyenPayoutError) {
        var vm = this;

        vm.adyenPayoutError = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            AdyenPayoutError.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
