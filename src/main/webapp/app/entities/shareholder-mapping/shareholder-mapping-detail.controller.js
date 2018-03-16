(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ShareholderMappingDetailController', ShareholderMappingDetailController);

    ShareholderMappingDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ShareholderMapping'];

    function ShareholderMappingDetailController($scope, $rootScope, $stateParams, previousState, entity, ShareholderMapping) {
        var vm = this;

        vm.shareholderMapping = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('adyenMiraklConnectorApp:shareholderMappingUpdate', function(event, result) {
            vm.shareholderMapping = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
