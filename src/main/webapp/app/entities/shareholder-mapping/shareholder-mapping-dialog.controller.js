(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ShareholderMappingDialogController', ShareholderMappingDialogController);

    ShareholderMappingDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ShareholderMapping'];

    function ShareholderMappingDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ShareholderMapping) {
        var vm = this;

        vm.shareholderMapping = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.shareholderMapping.id !== null) {
                ShareholderMapping.update(vm.shareholderMapping, onSaveSuccess, onSaveError);
            } else {
                ShareholderMapping.save(vm.shareholderMapping, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('adyenMiraklConnectorApp:shareholderMappingUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
