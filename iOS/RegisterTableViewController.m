//
//  RegisterTableViewController.m
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "RegisterTableViewController.h"
#import "CMApp.h"

@interface RegisterTableViewController ()

@end

@implementation RegisterTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return (section == 0) ? 4 : 1;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    
    cell.textLabel.textAlignment = NSTextAlignmentCenter;
    cell.textLabel.textColor = CM_CELL_BUTTON_COLOR;
    
    // Configure the cell...
    CGRect fieldFrame = CGRectMake(20, 10, tableView.frame.size.width - 20, 25);
    
    if (!username) {
        username = [[UITextField alloc] initWithFrame:fieldFrame];
        
        username.placeholder = @"Nombre de usuario";
    }
    
    if (!email) {
        email = [[UITextField alloc] initWithFrame:fieldFrame];
        
        email.placeholder = @"Email";
    }
    
    if (!password) {
        password = [[UITextField alloc] initWithFrame:fieldFrame];
        
        password.placeholder = @"Contraseña";
    }
    
    if (!password_confirmation) {
        password_confirmation = [[UITextField alloc] initWithFrame:fieldFrame];
        
        password_confirmation.placeholder = @"Confirmación de contraseña";
    }
    
    if (indexPath.section == 1 && indexPath.row == 0) {
        cell.textLabel.text = @"Registrar";
    } else if (indexPath.section == 0) {
        cell.textLabel.text = nil;
        
        switch (indexPath.row) {
            case 0:
                [cell addSubview:username];
                break;
            case 1:
                [cell addSubview:email];
                break;
            case 2:
                [cell addSubview:password];
                break;
            case 3:
                [cell addSubview:password_confirmation];
                break;
            default:
                break;
        }
    }
    
    return cell;
}

- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict{
    
    NSLog(@"funciona");
    [self.navigationController dismissViewControllerAnimated:true completion:nil];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    if (indexPath.row == 0 && indexPath.section == 1 ) {
        
        CMCoreService * reg = [CMCoreService sharedInstance];
        
        [reg setDelegate:self];
        [reg registerWithUsername:username.text password:password.text email:email.text];
        
    }
}




/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
