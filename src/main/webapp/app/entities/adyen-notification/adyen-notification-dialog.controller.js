(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenNotificationDialogController', AdyenNotificationDialogController);

    AdyenNotificationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'AdyenNotification'];

    function AdyenNotificationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, AdyenNotification) {
        var vm = this;

        vm.adyenNotification = entity;
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
            if (vm.adyenNotification.id !== null) {
                AdyenNotification.update(vm.adyenNotification, onSaveSuccess, onSaveError);
            } else {
                AdyenNotification.save(vm.adyenNotification, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('adyenMiraklConnectorApp:adyenNotificationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
