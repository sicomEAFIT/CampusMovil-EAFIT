//
//  MenuTableViewController.h
//  CampusMovil
//
//  Created by Daniel Klinkert on 8/8/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <SlideNavigationController.h>

@interface MenuTableViewController : UITableViewController{
    
    NSMutableArray * markers;
    NSMutableArray * dataSource;
}
@property (nonatomic, assign) BOOL slideOutAnimationEnabled;

@end
