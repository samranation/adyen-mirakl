(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenPayoutErrorDialogController', AdyenPayoutErrorDialogController);

    AdyenPayoutErrorDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'AdyenPayoutError'];

    function AdyenPayoutErrorDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, AdyenPayoutError) {
        var vm = this;

        vm.adyenPayoutError = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
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
            if (vm.adyenPayoutError.id !== null) {
                AdyenPayoutError.update(vm.adyenPayoutError, onSaveSuccess, onSaveError);
            } else {
                AdyenPayoutError.save(vm.adyenPayoutError, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('adyenMiraklConnectorApp:adyenPayoutErrorUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.createdAt = false;
        vm.datePickerOpenStatus.updatedAt = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
