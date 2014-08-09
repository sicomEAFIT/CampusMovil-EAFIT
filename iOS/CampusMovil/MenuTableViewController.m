
//
//  MenuTableViewController.m
//  CampusMovil
//
//  Created by Daniel Klinkert on 8/8/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "MenuTableViewController.h"

@interface MenuTableViewController ()

@end

@implementation MenuTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    CMCoreService * markerService = [[CMCoreService alloc] init];
    [markerService setDelegate:self];
    [markerService bringAllMarkers];
    
    
    markers = [[NSMutableDictionary alloc] init];
    
    dataSource = [[NSMutableArray alloc] initWithObjects:@"Suggestions",@"About Us",@"LogOut", nil];
   // markers = [[NSMutableArray alloc]initWithObjects:@"marker1", nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
	self.slideOutAnimationEnabled = YES;
	
	return [super initWithCoder:aDecoder];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    if (section == 0 ) {
        return [markers.allKeys count];
    }else{
        return [dataSource count];
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section{
    
    UILabel * title = [[UILabel alloc] initWithFrame:CGRectMake(6, 3, 136, 2)];
    [title setTextAlignment:NSTextAlignmentRight];
    
    if (section == 0) {
        [title setText:@"Markers"];
    }else{
        [title setText:@"Options"];
    }
    
    return title;
    
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 30;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    
    // Configure the cell...
    
    if (indexPath.section == 0){
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        [cell.textLabel setText:[markers.allKeys objectAtIndex:indexPath.row]];
        
    }else{
        
        [cell.textLabel setText:[dataSource objectAtIndex:indexPath.row]];
    }
    
    [cell.textLabel setTextAlignment:NSTextAlignmentRight];
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    UIStoryboard *main= [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    
    UIViewController *vc = [main instantiateViewControllerWithIdentifier:@"DetailController"];


    [[SlideNavigationController sharedInstance] pushViewController:vc animated:true];
    
}

#pragma Service

- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict{
    
    for (NSDictionary *allMarkers in dict) {
       
        
        [markers setObject:[allMarkers objectForKey:@"title"] forKey:[allMarkers objectForKey:@"subtitle"]];
        
    }
    [self.tableView reloadData];
    
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
