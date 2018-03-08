(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ProcessEmailDetailController', ProcessEmailDetailController);

    ProcessEmailDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'ProcessEmail'];

    function ProcessEmailDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, ProcessEmail) {
        var vm = this;

        vm.processEmail = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('adyenMiraklConnectorApp:processEmailUpdate', function(event, result) {
            vm.processEmail = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
