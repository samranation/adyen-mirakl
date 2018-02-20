(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenNotificationController', AdyenNotificationController);

    AdyenNotificationController.$inject = ['DataUtils', 'AdyenNotification'];

    function AdyenNotificationController(DataUtils, AdyenNotification) {

        var vm = this;

        vm.adyenNotifications = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadAll();

        function loadAll() {
            AdyenNotification.query(function(result) {
                vm.adyenNotifications = result;
                vm.searchQuery = null;
            });
        }
    }
})();
