(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('MiraklDeltaDialogController', MiraklDeltaDialogController);

    MiraklDeltaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'MiraklDelta'];

    function MiraklDeltaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, MiraklDelta) {
        var vm = this;

        vm.miraklDelta = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.miraklDelta.id !== null) {
                MiraklDelta.update(vm.miraklDelta, onSaveSuccess, onSaveError);
            } else {
                MiraklDelta.save(vm.miraklDelta, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('adyenMiraklConnectorApp:miraklDeltaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.shopDelta = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
