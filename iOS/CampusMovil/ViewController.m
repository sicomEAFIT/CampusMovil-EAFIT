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
//    if (![[NSUserDefaults standardUserDefaults] objectForKey:@"user"]) {
//        [self performSegueWithIdentifier:@"login_segue" sender:nil];
//    }
    
    [GMSServices provideAPIKey:@"AIzaSyC9_DsDPl74mP4SUa9Zd1XNaB1nE0bPcYg"];
    
    GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:6.195100
                                                            longitude:-75.5631196
                                                                 zoom:15];
    [mapview setDelegate:self];
    mapview = [GMSMapView mapWithFrame:CGRectZero camera:camera];
    mapview.myLocationEnabled = YES;
    self.view = mapview;
    
    // Creates a marker in the center of the map.
    GMSMarker *marker = [[GMSMarker alloc] init];
    marker.position = CLLocationCoordinate2DMake(6.195100 , -75.5631196);
    marker.title = @"Mi casa Perro";
    marker.snippet = @"Colombia";
    marker.map = mapview;
    
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
