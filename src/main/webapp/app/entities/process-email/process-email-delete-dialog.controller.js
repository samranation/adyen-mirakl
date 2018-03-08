(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ProcessEmailDeleteController',ProcessEmailDeleteController);

    ProcessEmailDeleteController.$inject = ['$uibModalInstance', 'entity', 'ProcessEmail'];

    function ProcessEmailDeleteController($uibModalInstance, entity, ProcessEmail) {
        var vm = this;

        vm.processEmail = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ProcessEmail.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
