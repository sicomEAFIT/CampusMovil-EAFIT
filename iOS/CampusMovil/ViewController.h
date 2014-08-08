//
//  ViewController.h
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CMCoreService.h"
#import <SlideNavigationController.h>
#import <GoogleMaps/GoogleMaps.h>

#if TARGET_OS_IPHONE
    #import <AudioToolbox/AudioServices.h>
#endif


@interface ViewController : UIViewController <GMSMapViewDelegate, CMCoreServicesDelgate,SlideNavigationControllerDelegate> {
    GMSMapView*mapview;
}


@end

