//
//  LoginTableViewController.m
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/3/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "LoginTableViewController.h"
#import "CMApp.h"

@interface LoginTableViewController ()

@end

@implementation LoginTableViewController

- (void)viewDidLoad {
    [self.tableView setBackgroundColor:CM_TINT_TABLE_BACKGROUND];
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
    return 3;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return (section > 0) ? 1 : 2;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    
    cell.textLabel.textAlignment = NSTextAlignmentCenter;
    cell.textLabel.textColor = CM_CELL_BUTTON_COLOR;
    
    CGRect fieldFrame = CGRectMake(20, 10, tableView.frame.size.width - 20, 25);
    
    if (!username) {
        username = [[UITextField alloc] initWithFrame:fieldFrame];
        
        username.placeholder = @"Nombre de usuario";
    }
    
    if (!password) {
        password = [[UITextField alloc] initWithFrame:fieldFrame];
        
        password.placeholder = @"Contraseña";
        password.secureTextEntry = true;
    }
    
    if (indexPath.section == 0) {
        cell.textLabel.text = nil;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        switch (indexPath.row) {
            case 0:
                [cell addSubview:username];
                break;
            case 1:
                [cell addSubview:password];
                break;
            default:
                break;
        }
    } else if (indexPath.section == 1) {
        cell.textLabel.text = @"Login";
    } else {
        cell.textLabel.text = @"Registro";
    }
    
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return (section > 0) ? nil : @"LOGIN";
}

- (NSString *)tableView:(UITableView *)tableView titleForFooterInSection:(NSInteger)section {
    NSString * footer = @"No es necesario ser estudiante activo de la Universidad EAFIT, los visitantes también pueden hacer el registro con un email valido";
    
    return (section == 2) ? footer : nil;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 44;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 1 && indexPath.row == 0) {
        [self login];
    } else if (indexPath.section == 2 && indexPath.row == 0) {
        [self performSegueWithIdentifier:@"register_segue" sender:nil];
    }
}

- (void)login {
    CMCoreService * login = [CMCoreService sharedInstance];
    
    [login setDelegate:self];
    NSLog(@"hola");
    [login loginWithUsername:username.text password:password.text];
}

- (void)CMCore:(CMCoreService *)cm didError:(NSError *)error {
    [[[UIAlertView alloc] initWithTitle:@"Error" message:error.localizedDescription delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
}

- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict {
    if ([[dict objectForKey:@"success"] boolValue] == true) {
        [[NSUserDefaults standardUserDefaults] setObject:[dict objectForKey:@"auth"] forKey:@"auth"];
        
        [self performSegueWithIdentifier:@"loginToMap_Segue" sender:nil];
    }
}

- (void)dispatchAlertWithError:(NSString *)error {
    [[[UIAlertView alloc] initWithTitle:@"Error"
                                message:error
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
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
