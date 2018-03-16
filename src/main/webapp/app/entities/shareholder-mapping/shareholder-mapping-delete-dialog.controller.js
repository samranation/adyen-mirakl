(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ShareholderMappingDeleteController',ShareholderMappingDeleteController);

    ShareholderMappingDeleteController.$inject = ['$uibModalInstance', 'entity', 'ShareholderMapping'];

    function ShareholderMappingDeleteController($uibModalInstance, entity, ShareholderMapping) {
        var vm = this;

        vm.shareholderMapping = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ShareholderMapping.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
