//
//  LoginTableViewController.h
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CMCoreService.h"

@interface LoginTableViewController : UITableViewController <CMCoreServicesDelgate>
{
    UITextField * username;
    UITextField * password;
}
@end
