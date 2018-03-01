(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('MiraklDeltaDetailController', MiraklDeltaDetailController);

    MiraklDeltaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'MiraklDelta'];

    function MiraklDeltaDetailController($scope, $rootScope, $stateParams, previousState, entity, MiraklDelta) {
        var vm = this;

        vm.miraklDelta = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('adyenMiraklConnectorApp:miraklDeltaUpdate', function(event, result) {
            vm.miraklDelta = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
