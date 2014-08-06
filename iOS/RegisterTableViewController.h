//
//  RegisterTableViewController.h
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CMCoreService.h"

@interface RegisterTableViewController : UITableViewController<CMCoreServicesDelgate>
{
    UITextField * username;
    UITextField * password;
    UITextField * password_confirmation;
    UITextField * email;
}

@end
