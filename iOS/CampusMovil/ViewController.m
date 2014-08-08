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
    
    
}

- (void)showGoogleMaps {
    GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:6.200396
                                                            longitude:-75.578698
                                                                 zoom:18];
    
    mapview = [GMSMapView mapWithFrame:CGRectZero camera:camera];
    
    [mapview setDelegate:self];
    [mapview setMinZoom:16 maxZoom:20];
    [mapview setMapType:kGMSTypeSatellite];
    [mapview setIndoorEnabled:false];
    
    self.view = mapview;
}

- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict {
    
    //[GMSServices provideAPIKey:@"AIzaSyC9_DsDPl74mP4SUa9Zd1XNaB1nE0bPcYg"];
    
    for (NSDictionary *markers in dict) {
        GMSMarker *marker = [[GMSMarker alloc]init];
        double la=[[markers objectForKey:@"latitude"] doubleValue];
        double lo=[[markers objectForKey:@"longitude"] doubleValue];
        
        CLLocation * loca=[[CLLocation alloc]initWithLatitude:la longitude:lo];
        CLLocationCoordinate2D coordi=loca.coordinate;
        
        marker=[GMSMarker markerWithPosition:coordi];
        [marker setTitle:[markers objectForKey:@"title"]];
        marker.snippet = [markers objectForKey:@"subtitle"];
        marker.map = mapview;
        
    }
}


- (BOOL)slideNavigationControllerShouldDisplayRightMenu{
    
    return true;
}
- (void)mapView:(GMSMapView *)mapView didChangeCameraPosition:(GMSCameraPosition *)position {
    AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
}

- (void)CMCore:(CMCoreService *)cm didError:(NSError *)error{
    //NSLog(@"eror");
    
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
