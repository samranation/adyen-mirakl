(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ProcessEmailDialogController', ProcessEmailDialogController);

    ProcessEmailDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'ProcessEmail'];

    function ProcessEmailDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, ProcessEmail) {
        var vm = this;

        vm.processEmail = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.processEmail.id !== null) {
                ProcessEmail.update(vm.processEmail, onSaveSuccess, onSaveError);
            } else {
                ProcessEmail.save(vm.processEmail, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('adyenMiraklConnectorApp:processEmailUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
