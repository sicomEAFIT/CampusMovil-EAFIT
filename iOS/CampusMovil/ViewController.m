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

    [super viewDidLoad];
//    if ([[NSUserDefaults standardUserDefaults] objectForKey:@"auth"]) {
//        [self performSegueWithIdentifier:@"login_segue" sender:nil];
//    }
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)viewDidAppear:(BOOL)animated {
    CMCoreService * markers = [CMCoreService sharedInstance];
    [markers setDelegate:self];
    [markers bringAllMarkersWithUserName:@"a"];
    
   }

-(void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict {
    
    [GMSServices provideAPIKey:@"AIzaSyC9_DsDPl74mP4SUa9Zd1XNaB1nE0bPcYg"];
    
    
    GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:6.200396
                                                            longitude:-75.578698
                                                                 zoom:16];
    [mapview setDelegate:self];
    mapview = [GMSMapView mapWithFrame:CGRectZero camera:camera];
    [mapview setMapType:kGMSTypeHybrid];
    self.view = mapview;
    
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
    
    
    //[[dic objectAtIndex:0] objectForKey:@"latitude"];
    
//            GMSMarker *marker = [[GMSMarker alloc]init];
//           marker.position = CLLocationCoordinate2DMake((double)[[dic objectAtIndex:0] objectForKey:@"latitude"], (double)[[dic objectAtIndex:0]objectForKey:@"longitude"]);
//           [marker setTitle:[[dic objectAtIndex:0]objectForKey:@"title"] ];
//           [marker setSnippet:[[dic objectAtIndex:0]objectForKey:@"subtitle"]];
//           NSLog(@"%@",marker.title);
//           marker.map = mapview;

}

- (void)CMCore:(CMCoreService *)cm didError:(NSError *)error{
    NSLog(@"eror");
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
