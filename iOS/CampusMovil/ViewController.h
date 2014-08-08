//
//  ViewController.h
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CMCoreService.h"
#import <GoogleMaps/GoogleMaps.h>

@interface ViewController : UIViewController<GMSMapViewDelegate,CMCoreServicesDelgate>{
    GMSMapView*mapview;
}


@end

