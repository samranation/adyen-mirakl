(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('MiraklDeltaDeleteController',MiraklDeltaDeleteController);

    MiraklDeltaDeleteController.$inject = ['$uibModalInstance', 'entity', 'MiraklDelta'];

    function MiraklDeltaDeleteController($uibModalInstance, entity, MiraklDelta) {
        var vm = this;

        vm.miraklDelta = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            MiraklDelta.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
