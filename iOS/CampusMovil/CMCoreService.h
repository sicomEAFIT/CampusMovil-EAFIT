//
//  CMCoreService.h
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/5/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <Foundation/Foundation.h>
#define CM_URL_MAKER(A) [NSString stringWithFormat:@"http://campusmovilapp.heroapp.com/%@", A]
#define CM_API_V1_AUTH @"api/v1/auth"


@class CMCoreService;
@protocol CMCoreServicesDelgate <NSObject>
@required
- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict;
@optional
- (void)CMCore:(CMCoreService *)cm didError:(NSError *)error;
@end

@interface CMCoreService : NSObject
@property (nonatomic, weak) id<CMCoreServicesDelgate> delegate;
@property (nonatomic, strong, readonly) NSMutableURLRequest * request;

+ (id)sharedInstance;

- (instancetype)init;

- (NSData *)request:(NSDictionary *)dic;
- (void)loginWithUsername:(NSString *)username password:(NSString *)password;
- (void)registerWithUsername:(NSString *)username password:(NSString *)password email:(NSString *)email;

@end
