//
//  AppDelegate.m
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "AppDelegate.h"
#import "CMApp.h"

@interface AppDelegate ()

@end

@implementation AppDelegate
            

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    
    self.window.tintColor = CM_TINT_BAR_COLOR;
    
    [[UINavigationBar appearance] setTintColor:CM_TINT_COLOR];
    [[UINavigationBar appearance] setBarTintColor:CM_TINT_BAR_COLOR];
    
    NSShadow *shadow = [[NSShadow alloc] init];
    shadow.shadowColor = [UIColor lightGrayColor];
    shadow.shadowBlurRadius = 0.5;
    shadow.shadowOffset = CGSizeMake(0.0, 2.0);
    
    [[UINavigationBar appearance] setTitleTextAttributes: @{
        NSForegroundColorAttributeName : [UIColor whiteColor],
        NSFontAttributeName : [UIFont fontWithName:@"Futura-CondensedExtraBold" size:20],
        NSShadowAttributeName : shadow
    }];
    
    if (![GMSServices provideAPIKey:CM_GMAP_API_KEY]) {
        NSLog(@"Fallo el registro del API-KEY de Google Maps");
    }
    
    // Override point for customization after application launch.
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
