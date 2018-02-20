(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('adyen-notification', {
            parent: 'entity',
            url: '/adyen-notification',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.adyenNotification.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/adyen-notification/adyen-notifications.html',
                    controller: 'AdyenNotificationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('adyenNotification');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('adyen-notification-detail', {
            parent: 'adyen-notification',
            url: '/adyen-notification/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.adyenNotification.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/adyen-notification/adyen-notification-detail.html',
                    controller: 'AdyenNotificationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('adyenNotification');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'AdyenNotification', function($stateParams, AdyenNotification) {
                    return AdyenNotification.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'adyen-notification',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('adyen-notification-detail.edit', {
            parent: 'adyen-notification-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-notification/adyen-notification-dialog.html',
                    controller: 'AdyenNotificationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AdyenNotification', function(AdyenNotification) {
                            return AdyenNotification.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('adyen-notification.new', {
            parent: 'adyen-notification',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-notification/adyen-notification-dialog.html',
                    controller: 'AdyenNotificationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                rawAdyenNotification: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('adyen-notification', null, { reload: 'adyen-notification' });
                }, function() {
                    $state.go('adyen-notification');
                });
            }]
        })
        .state('adyen-notification.edit', {
            parent: 'adyen-notification',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-notification/adyen-notification-dialog.html',
                    controller: 'AdyenNotificationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AdyenNotification', function(AdyenNotification) {
                            return AdyenNotification.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('adyen-notification', null, { reload: 'adyen-notification' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('adyen-notification.delete', {
            parent: 'adyen-notification',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-notification/adyen-notification-delete-dialog.html',
                    controller: 'AdyenNotificationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['AdyenNotification', function(AdyenNotification) {
                            return AdyenNotification.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('adyen-notification', null, { reload: 'adyen-notification' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
