//
//  ViewController.m
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "ViewController.h"


@interface ViewController ()

@end

@implementation ViewController


- (void)viewDidLoad {
    
    
    [self showGoogleMaps];
    
    
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)viewDidAppear:(BOOL)animated {
    
    UINavigationController *nav = (UINavigationController *)[[SlideNavigationController sharedInstance] rightMenu];
    [nav popToRootViewControllerAnimated:true];
    
    if (![CMCoreService isUserLogged]) {
        [self performSegueWithIdentifier:@"login_segue" sender:nil];
    }
    
    CMCoreService * markers = [CMCoreService sharedInstance];
    [markers setDelegate:self];
    [markers bringAllMarkers];
    
    NSLog(@"User: %@", markers.user.username);
    
    [super viewDidAppear:animated];
}

- (void)showGoogleMaps {
    GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:6.200396
                                                            longitude:-75.578698
                                                                 zoom:18];
    
//    GMSCoordinateBounds *mapBounds = [[GMSCoordinateBounds alloc] initWithCoordinate:CLLocationCoordinate2DMake(6.1932748, -75.5823696) coordinate:CLLocationCoordinate2DMake(6.203500, -75.577057)];
    
    mapview = [GMSMapView mapWithFrame:CGRectZero camera:camera];
    
    [mapview setDelegate:self];
    [mapview setMinZoom:16 maxZoom:20];
    [mapview setMapType:kGMSTypeSatellite];
    [mapview setIndoorEnabled:false];
    
//    [mapview animateWithCameraUpdate:[GMSCameraUpdate fitBounds:mapBounds withPadding:10]];
    
    self.view = mapview;
}

- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict {
    
    
    for (NSDictionary *markers in dict) {
        GMSMarker *marker;
        
        double la=[[markers objectForKey:@"latitude"] doubleValue];
        double lo=[[markers objectForKey:@"longitude"] doubleValue];
        
        CLLocation * loca=[[CLLocation alloc]initWithLatitude:la longitude:lo];
        CLLocationCoordinate2D coordi=loca.coordinate;
        
        marker=[GMSMarker markerWithPosition:coordi];
        [marker setTitle:[markers objectForKey:@"title"]];
        marker.snippet = [markers objectForKey:@"subtitle"];
        [marker setIcon:[UIImage imageNamed:@"map_marker"]];
        marker.map = mapview;
        

    }
}

- (BOOL)slideNavigationControllerShouldDisplayRightMenu{
    return true;
}



- (void)mapView:(GMSMapView *)mapView didChangeCameraPosition:(GMSCameraPosition *)position {
    if ((position.target.latitude > 6.1932748 && position.target.longitude < -75.5823696)
        || (position.target.latitude < 6.203500 && position.target.longitude > -75.577057)) {
        
        AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
        
        GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:6.200396
                                                                longitude:-75.578698
                                                                     zoom:18];
        
        [mapview animateToCameraPosition:camera];
    }
    
}

- (void)CMCore:(CMCoreService *)cm didError:(NSError *)error{
    
    [[[UIAlertView alloc] initWithTitle:@"Error"
                                message:error.localizedDescription
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles: nil] show];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
